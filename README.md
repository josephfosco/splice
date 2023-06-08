# splice

FIXME: description

## Installation

When cloning this repo, if you use git clone --recurse-submodules <repository> git will populate the submodule(s) for you

### Submodules
- To make using the submodule(s) easier, you might want to set the following git config options
    - `git config status.submodulesummary 1`
    - `git config push.recurseSubmodules check`


## Usage

### To start playing in a terminal
- Open qjackctl in a window and go to the "Graph" page
- in a new Terminal window in the splice directory type `make sc`
- Go to the qjackctl window and connect output 1 and 2 from the "Overtone" component/server to
  the input 1 and 2 of the computer speakers
- In another Terminal window in the splice directory type `clj` to bring up the Clojure repl
- In the clojure repl at the user=> prompt type`clj`
- at the splice.core=> prompt type `(splice-start)`
- To stop, in the clojure window type `(splice-stop)`
 
### To run this form emacs using cider: - not certain if this is still accurate
- Open qjackctl in a window and go to the "Graph" page
- in a new Terminal window type `make cider`
- Go to the qjackctl window and connect output 1 and 2 from the "Overtone" component/server to
  the input 1 and 2 of the computer speakers
- In emacs type M-x cider-connect
- Just hit <enter> at any prompts
- A clojure repl will open in an new emacs buffer. This repl is connected to the clojure server you started above
- Type `(ns core.splice)` in the emacs clojure session
- Type `(splice-start)` in the emacs clojure repl to start plating
- Type `(splice-stop)` in the emacs clojure repl to stop playing
- Type C-x k to quit the emacs cider repl. When emacs asks if you want to kill the active process type `y`
 
### Note: when defining instruments 
- :freq should always be the first param 
- :vol should always be the second param
- :release should be the param name for :envelope release time in secs

## Options

- the splice-start command has the following optional options
    - :loops <loops filename relative to splice directory> 
    If not specified, the file splice/src/loops/loops.clj will be used
    - :osc-debug ( true | [false] )
    To print additional debugging info from Open Sound Control (OSC)

## Examples

To run tests
  Start a REPL
  Load all tests from the test directory (you can do this
    by loading test/transit/core_test.clj)
  (clojure.test/run-all-tests #"splice.*")
  
To run tests in one namespace
  (clojure.test/run-tests '<namespace-test>)

...

### Bugs

...

### Any Other Sections
### That You Think
### Might be Useful

## License

Copyright © 2017-2022 Joseph Fosco. All Rights Reserved

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

