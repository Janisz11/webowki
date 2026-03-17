<?php

if (!defined('ABSPATH')) exit;

class SystemOgloszenPro {
    public function __construct() {
        add_action('init', [$this, 'rejestruj_cpt']);
        add_filter('the_content', [$this, 'wyswietl_reklame']);
        add_action('wp_ajax_rejestruj_klikniecie', [$this, 'obsluga_klikniecia']);
        add_action('wp_ajax_nopriv_rejestruj_klikniecie', [$this, 'obsluga_klikniecia']);
        add_filter('manage_ogloszenie_posts_columns', [$this, 'kolumny_admina']);
        add_action('manage_ogloszenie_posts_custom_column', [$this, 'wartosci_kolumn'], 10, 2);
    }

    public function rejestruj_cpt() {
        register_post_type('ogloszenie', [
            'labels' => ['name' => 'Ogłoszenia', 'singular_name' => 'Ogłoszenie'],
            'public' => true,
            'menu_icon' => 'dashicons-megaphone',
            'supports' => ['title', 'editor'],
        ]);
    }

    public function wyswietl_reklame($content) {
        static $doing_it = false;
        if ($doing_it || !is_singular('post') || !in_the_loop() || !is_main_query()) return $content;
        $doing_it = true;
        $ads = get_posts(['post_type' => 'ogloszenie', 'posts_per_page' => 1, 'orderby' => 'rand', 'post_status' => 'publish']);
        if ($ads) {
            $ad = $ads[0];
            update_post_meta($ad->ID, '_views', (int)get_post_meta($ad->ID, '_views', true) + 1);
            $ad_body = html_entity_decode($ad->post_content, ENT_QUOTES, 'UTF-8');
            $ad_html = "<div class='janisz-ad' data-id='{$ad->ID}' style='border:4px solid #000; padding:20px; margin:20px 0; background:#ffff00; cursor:pointer;'>
                <div style='font-size:10px; text-transform:uppercase; font-weight:bold; margin-bottom:10px;'>Sponsorowane</div>
                <div class='ad-render'>" . do_shortcode($ad_body) . "</div>
            </div>
            <script>
            document.addEventListener('DOMContentLoaded', function() {
                const c = document.querySelector('.janisz-ad[data-id=\"{$ad->ID}\"]');
                if(!c) return;
                c.addEventListener('click', function(e) {
                    const typ = e.target.closest('.janisz-btn') ? 'button' : 'tlo';
                    fetch('" . admin_url('admin-ajax.php') . "', {
                        method: 'POST',
                        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                        body: 'action=rejestruj_klikniecie&ad_id={$ad->ID}&typ_kliku=' + typ
                    });
                });
            });
            </script>";
            $doing_it = false;
            return $ad_html . $content;
        }
        $doing_it = false;
        return $content;
    }

    public function obsluga_klikniecia() {
        $id = intval($_POST['ad_id']);
        $meta = ($_POST['typ_kliku'] === 'button') ? '_btn_clicks' : '_clicks';
        update_post_meta($id, $meta, (int)get_post_meta($id, $meta, true) + 1);
        wp_die();
    }

    public function kolumny_admina($c) {
        return array_merge($c, ['views'=>'Odsłony', 'clicks'=>'Klik (Tło)', 'btn_clicks'=>'Klik (Button)']);
    }

    public function wartosci_kolumn($c, $id) {
        if (in_array($c, ['views', 'clicks', 'btn_clicks'])) echo get_post_meta($id, '_'.$c, true) ?: 0;
    }
}
new SystemOgloszenPro();