# Simmr Android modularization

## Current graph

```text
:app
 ├── :feature:onboarding
 └── :core:designsystem

:feature:onboarding
 └── :core:designsystem

:feature:designsystem
 └── :core:designsystem
```

Dependencies point inward: application shell → features → core. Core modules never
depend on a feature or on the application module.

## Module responsibilities

- `:app` owns process-level setup, the activity, app theme installation, and the
  future navigation host. It should contain no screen implementation.
- `:core:designsystem` owns brand resources, design tokens, typography, theme,
  and reusable UI primitives. It contains no feature-specific behavior.
- `:feature:<name>` owns one user-facing area, including its screen composables,
  state holder, and feature-specific UI. A feature may depend on core modules but
  should not directly depend on another feature.

## Planned feature mapping

The iOS app maps naturally to independent Android modules:

- `:feature:onboarding` (implemented)
- `:feature:home`
- `:feature:recipeoverview`
- `:feature:cookingmode`
- `:feature:unitconverter`
- `:feature:settings`

Shared domain models, persistence, and network implementations should be added as
focused `:core:model`, `:core:data`, and `:core:network` modules only when the
first feature needs them. This avoids empty architecture scaffolding.

## Boundary rules

1. Keep feature entry points public and implementation details internal.
2. Pass navigation callbacks into a feature instead of importing app navigation.
3. Promote UI to `:core:designsystem` only when it is brand-level or reused.
4. Promote business models to `:core:model` only when multiple features share them.
5. Add each module explicitly to `settings.gradle.kts` and verify it independently.
