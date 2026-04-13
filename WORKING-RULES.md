# FuelTrack Working Rules

This file defines the practical working rules for developing FuelTrack.

The goal is to keep development professional, traceable, and safe without creating noisy history or artificial process overhead.

## 1. Core Principle

Use version control to serve:

- safety
- traceability
- recoverability
- professionalism

Do not use commits and pushes as empty activity.

## 2. Commit Rule

Commit at meaningful, stable checkpoints.

A commit should be:

- logically coherent
- buildable
- understandable in isolation
- reviewable
- reversible

Good commit boundaries include:

- one feature slice
- one bug fix
- one UI pass on one screen
- one refactor with one clear purpose
- one documentation or governance update set

Do not commit:

- broken builds
- half-finished experiments
- unrelated mixed changes
- temporary debug edits
- vague work that cannot be explained cleanly

## 3. Push Rule

Push after meaningful, reviewable milestones or before risky work.

A push should happen when:

- the branch has reached a useful checkpoint
- the code is in a recoverable state
- there is value in backing it up remotely
- the current state would be acceptable to show in a real repository history

Do not push just for the sake of frequent activity.

Do not push broken or ambiguous intermediate states unless there is a deliberate reason and it is clearly isolated.

## 4. Commit Message Standard

Commit messages must be professional, focused, and readable.

Use short imperative subject lines.

Good examples:

- `Refine add fuel entry modal styling`
- `Convert end trip flow to modal presentation`
- `Fix dashboard active trip navigation mismatch`
- `Add trip history summary cards`

Avoid:

- `changes`
- `more work`
- `fix stuff`
- `update`

## 5. Branching Guidance

For FuelTrack:

- keep `main` stable and runnable
- use direct commits to `main` only for small disciplined updates
- use short-lived feature or fix branches for larger work

Example branch names:

- `feature/trip-workspace-polish`
- `feature/localization-pass`
- `fix/dashboard-trip-state`

## 6. Before Commit Checklist

Before committing:

- the build should pass
- relevant tests should pass when applicable
- unrelated files should not be included
- the commit scope should match the message
- documentation should be updated if the change affects architecture, workflow, or presentation

For UI-only changes:

- the app should at least build successfully
- the changed screen or flow should be checked manually when practical

## 7. Before Push Checklist

Before pushing:

- commit history should still be clean enough to keep
- no accidental local junk should be included
- the branch name should be reasonable
- the current checkpoint should be something worth preserving remotely

## 8. Professional Standard For This Project

FuelTrack should be developed in a way that reflects real-world software practice:

- focused commits
- meaningful pushes
- stable checkpoints
- clear version history
- no artificial noise

The standard is not maximum frequency.

The standard is high-quality checkpoints.
