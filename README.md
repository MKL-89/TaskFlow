# TaskFlow 📋

A simple and intuitive Android app for creating, managing, and tracking your tasks — all in one place. Built with a sleek dark theme and real-time search.

---

## Features

- ✅ **Create & Manage Tasks** — Add, edit, and delete tasks with ease
- 🔍 **Real-Time Search** — Filter tasks instantly as you type
- 📊 **Track Progress** — Stay on top of what's done and what's pending
- 🌙 **Dark Theme UI** — Navy gradient background, dark cards, white fonts, purple checkboxes

---

## UI & Design

The following layout files define the app's dark themed interface:

| File | Description |
|---|---|
| `activity_main.xml` | Dark background, TaskFlow title and subtitle |
| `cool_wallpaper.xml` | Navy gradient drawable background |
| `item_task.xml` | Dark cards, white fonts, purple checkbox |
| `dialog_task.xml` | Dark themed dialog for adding/editing tasks |

---

## Project Structure

```
TaskFlow/
├── app/
│   └── src/main/res/
│       ├── layout/
│       │   ├── activity_main.xml
│       │   ├── item_task.xml
│       │   └── dialog_task.xml
│       └── drawable/
│           └── cool_wallpaper.xml
├── gradle/
├── build.gradle
└── settings.gradle
```

---

## Known Issues / TODO

- [ ] Fix "0 tasks remaining" bug
- [ ] Fix dialog white bars
- [x] Add real-time search bar
- [ ] Push each feature as a separate commit

---

## Contributing

Contributions are welcome! Here's how to get started:

1. **Fork** the repository
2. **Create a branch** for your feature
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Commit** your changes with a clear message
   ```bash
   git commit -m "Feature: describe what you added"
   ```
4. **Push** to your fork and open a **Pull Request**

Please make sure your code is clean and tested before submitting a PR.

---

## License

This project is private. All rights reserved.
