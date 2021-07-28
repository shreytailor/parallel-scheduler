# Git Conventions

## Commit Flags

In our first group meeting, to discuss the client requirements and project scope, we had decided to adhere to a few conventions for the rest of the project. These are the *commit flags* we will be using for this repository.

- `Wiki` - for anything relating to the project documentation i.e. files within the `/wiki` folder.
- `Feature` - implementing a new feature, or a sub-feature.
- `Bug` - fixing a bug.
- `Refactor` - reformatting the code without changing the functionality.
- `Test` - commits relating to creating, deleting, modifying the testing entities.
- `Docs` - documentation within the codebase.
- `Performance` - improving the performance.

**Usage:** These commit flags would be used in accordance with square brackets, along with descriptive commit title and description. An example is shown below.

```
[Test] Adding tests for Parser.
- The test suite is created such that all edge cases are satisfied.
```

## Branching

Branches will be used while working on parts of the application, and they will be merged with `main` once the whole feature is implemented. The common pull request conventions will also be followed.