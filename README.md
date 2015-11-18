# Brockman

![Kent Brockman](doc/kent-brockman.jpg "Kent Brockman")

Simple RSpec-inspired test reporter for clojure.test.

[![Clojars Project](http://clojars.org/com.semperos/brockman/latest-version.svg)](http://clojars.org/com.semperos/brockman)

[![Build Status](https://travis-ci.org/semperos/brockman.svg?branch=master)](https://travis-ci.org/semperos/brockman)

## Installation

Until this is released to a Maven repo:

```
lein install
```

Check the [project.clj](project.clj) for the version.

## Usage

In your test suite:

```clj
(semperos.brockman/test-reporter! :rspec)
```

Or you can simply require the appropriate reporting namespace, e.g. `(require 'semperos.brockman.rspec)`. The `test-reporter!` approach makes it more explicit that you're overriding Clojure defaults.

## Testing

```
lein test
```

## Todo's

To better match RSpec-style output, the results for testing vars could be rolled up to avoid printing duplicate test var and test context information. Depending on how this is done, this could make test output slower to appear on the console.

## License

Copyright © 2015 RentPath. All rights reserved.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
