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
            'labels' => ['name' => 'Og┼éoszenia', 'singular_name' => 'Og┼éoszenie'],
            'public' => true,
            'menu_icon' => 'dashicons-chart-bar',
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
            
         
            $views = (int)get_post_meta($ad->ID, '_views', true);
            update_post_meta($ad->ID, '_views', $views + 1);

            
            $ad_html = "
            <div class='janisz-ad' data-id='{$ad->ID}' style='border:4px solid #000; padding:20px; margin:20px 0; background:#ffff00; cursor:pointer;'>
                <div style='font-size:10px; text-transform:uppercase;'>Sponsorowane</div>
                " . html_entity_decode(wpautop($ad->post_content)) . "
            </div>
            <script>
            document.addEventListener('DOMContentLoaded', function() {
                const ad = document.querySelector('.janisz-ad[data-id=\"{$ad->ID}\"]');
                ad.addEventListener('click', function() {
                    fetch('" . admin_url('admin-ajax.php') . "', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                        body: 'action=rejestruj_klikniecie&ad_id={$ad->ID}'
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
        if ($id > 0) {
            $clicks = (int)get_post_meta($id, '_clicks', true);
            update_post_meta($id, '_clicks', $clicks + 1);
        }
        wp_die(); 
    }

    /
    public function kolumny_admina($columns) {
        $columns['views'] = 'Wy┼¢wietlenia';
        $columns['clicks'] = 'Klikni─Öcia';
        return $columns;
    }

    public function wartosci_kolumn($column, $post_id) {
        if ($column == 'views') echo get_post_meta($post_id, '_views', true) ?: 0;
        if ($column == 'clicks') echo get_post_meta($post_id, '_clicks', true) ?: 0;
    }
}
new SystemOgloszenPro();
