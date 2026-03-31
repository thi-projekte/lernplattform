# Contributing 

In this project there are some rules when it comes to contributing. They are in place to make
working together easier for everyone and keeping up the code quality throughout the whole development process.

## General rules

The contribution language is English. The code functions should be in English as well as any additional documentation.

Furthermore, another key rule is: The less Javadoc, the better. If a function needs a description of what it does the name is probably 
not good enough. If you have to describe what a certain code block does, it is not readable enough.

## Branching strategy

Branches should be named after their purpose.
A branch should always have a reference to the GitHub issue that it solves. 

- A new feature therefore should have this branch name: `feature/<issue-id>-<short-description>`
- A new bugfix should be `fix/<issue-id>-<short-description>`


## Commit messages

For commit messages we should stick to the [conventional commits standard](https://www.conventionalcommits.org/en/v1.0.0/).

This means every commit should have a prefix that says what has been done in this commit and a reference the touched component.

- implements a new feature (or part of it): `feat(component): <message>`
- Fixes a bug: `fix(component): <message>`
- Refactor in the codebase but no change in user functionality: `refactor(component): <message>`
- Code formatting or style changes: `style: <message>`
- Introduction of new tests: `test(component): <message>`

## Linting

To ensure the quality of our code we want to ensure it is properly formatted. 

For the frontend we use [prettier](https://prettier.io/). To format the code properly just run `npm run format:fix`.

In the backend we use spotless. Just run `./mwnw spotless:apply` on mac or `./mwnw.cmd spotless:apply` on windows to ensure proper formatting.

## Testing

The frontend should be tested manually. We do not implement any unit or component tests there as it increases complexity a lot
and often is useless in most cases.

For the backend on the other side we have a strict testing strategy:
- Every service should have at least 75% test coverage
- Every controller should be tested with tests that cover the exposes REST endpoints and their edgecases.

## Code Reviews

To ensure the quality and maintainability of our code, we want to perform code reviews. 
Every pull request first has to be reviewed by one of the other developers to spot possibly mistakes or improvements.