# schpec

[![Build Status](https://travis-ci.org/gfredericks/schpec.svg?branch=master)](https://travis-ci.org/gfredericks/schpec)

Schpec is a utility library for clojure.spec.

## Obtention

``` clojure
[com.gfredericks/schpec "0.1.0"]
```

## Things it has

In the `com.gfredericks.schpec` namespace:

- `xor`: like `s/or` but values can only match exactly one spec
- `excl-keys`: like `s/keys`, but does not allow extra keys

## Things it could have if it had them

It is currently empty but is intended to be a home for all manner of
things, such as:

- other common specs
- generators related to specs
- other helper functions/macros for defining specs
- whatever other feature you miss from
  [plumatic/schema](https://github.com/plumatic/schema)
- etc.

## Contributing

Pull requests! I welcome anything related to clojure.spec that is at
least half-baked. Sufficiently weird things will be relegated to
sufficiently obscure namespaces.

## License

Copyright © 2016 Gary Fredericks

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
