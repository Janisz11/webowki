const { createYoga, createSchema } = require('graphql-yoga')
const { createServer } = require('http')
const axios = require('axios')
const Database = require('better-sqlite3')

// Inicjalizacja bazy danych
const db = new Database('app.db')

// Tworzenie tabel
db.exec(`
  CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    email TEXT NOT NULL,
    login TEXT NOT NULL
  );

  CREATE TABLE IF NOT EXISTS todos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    completed INTEGER NOT NULL DEFAULT 0,
    user_id INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
  );
`)

// Seed danych z REST API jeśli baza jest pusta
async function seedIfEmpty() {
  const userCount = db.prepare('SELECT COUNT(*) as count FROM users').get()
  if (userCount.count > 0) return

  const usersRes = await axios.get('https://jsonplaceholder.typicode.com/users')
  const todosRes = await axios.get('https://jsonplaceholder.typicode.com/todos')

  const insertUser = db.prepare('INSERT INTO users (id, name, email, login) VALUES (?, ?, ?, ?)')
  const insertTodo = db.prepare('INSERT INTO todos (id, title, completed, user_id) VALUES (?, ?, ?, ?)')

  for (const u of usersRes.data) {
    insertUser.run(u.id, u.name, u.email, u.username)
  }
  for (const t of todosRes.data) {
    insertTodo.run(t.id, t.title, t.completed ? 1 : 0, t.userId)
  }

  console.log('Baza danych zasilona danymi z REST API')
}

const yoga = createYoga({
  schema: createSchema({
    typeDefs: `
      type ToDoItem {
        id: ID!
        title: String!
        completed: Boolean!
        user: User!
      }

      type User {
        id: ID!
        name: String!
        email: String!
        login: String!
        todos: [ToDoItem!]!
      }

      type Query {
        todos: [ToDoItem!]
        todo(id: ID!): ToDoItem
        users: [User!]
        user(id: ID!): User
      }

      type Mutation {
        addUser(name: String!, email: String!, login: String!): User!
        updateUser(id: ID!, name: String, email: String, login: String): User!
        deleteUser(id: ID!): Boolean!

        addTodo(title: String!, user_id: ID!): ToDoItem!
        updateTodo(id: ID!, title: String, completed: Boolean): ToDoItem!
        deleteTodo(id: ID!): Boolean!
      }
    `,
    resolvers: {
      Query: {
        users: () => db.prepare('SELECT * FROM users').all(),
        user: (_, args) => db.prepare('SELECT * FROM users WHERE id = ?').get(args.id),
        todos: () => db.prepare('SELECT * FROM todos').all(),
        todo: (_, args) => db.prepare('SELECT * FROM todos WHERE id = ?').get(args.id),
      },
      User: {
        todos: (parent) => db.prepare('SELECT * FROM todos WHERE user_id = ?').all(parent.id),
      },
      ToDoItem: {
        user: (parent) => db.prepare('SELECT * FROM users WHERE id = ?').get(parent.user_id),
        completed: (parent) => parent.completed === 1 || parent.completed === true,
      },
      Mutation: {
        addUser: (_, args) => {
          const result = db.prepare('INSERT INTO users (name, email, login) VALUES (?, ?, ?)').run(args.name, args.email, args.login)
          return db.prepare('SELECT * FROM users WHERE id = ?').get(result.lastInsertRowid)
        },
        updateUser: (_, args) => {
          const user = db.prepare('SELECT * FROM users WHERE id = ?').get(args.id)
          if (!user) throw new Error('User not found')
          const name = args.name ?? user.name
          const email = args.email ?? user.email
          const login = args.login ?? user.login
          db.prepare('UPDATE users SET name = ?, email = ?, login = ? WHERE id = ?').run(name, email, login, args.id)
          return db.prepare('SELECT * FROM users WHERE id = ?').get(args.id)
        },
        deleteUser: (_, args) => {
          const result = db.prepare('DELETE FROM users WHERE id = ?').run(args.id)
          return result.changes > 0
        },
        addTodo: (_, args) => {
          const result = db.prepare('INSERT INTO todos (title, completed, user_id) VALUES (?, 0, ?)').run(args.title, args.user_id)
          return db.prepare('SELECT * FROM todos WHERE id = ?').get(result.lastInsertRowid)
        },
        updateTodo: (_, args) => {
          const todo = db.prepare('SELECT * FROM todos WHERE id = ?').get(args.id)
          if (!todo) throw new Error('Todo not found')
          const title = args.title ?? todo.title
          const completed = args.completed !== undefined ? (args.completed ? 1 : 0) : todo.completed
          db.prepare('UPDATE todos SET title = ?, completed = ? WHERE id = ?').run(title, completed, args.id)
          return db.prepare('SELECT * FROM todos WHERE id = ?').get(args.id)
        },
        deleteTodo: (_, args) => {
          const result = db.prepare('DELETE FROM todos WHERE id = ?').run(args.id)
          return result.changes > 0
        },
      },
    },
  }),
})

const server = createServer(yoga)

seedIfEmpty().then(() => {
  server.listen(4001, () => console.log('Server is running on http://localhost:4001'))
})