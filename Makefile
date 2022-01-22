start:
	pw-jack clj -m core
	# pw-jack clj -M src/splice/core.clj

repl:
	pw-jack clj

cider:
	pw-jack clj -Sdeps '{:deps {cider/cider-nrepl {:mvn/version "0.25.2"}}}' -m nrepl.cmdline --interactive --middleware "[cider.nrepl/cider-middleware]"
server:
	clojure -X:repl-server

clear-err-logs:
	rm hs_err*.log

core-dump-off:
	ulimit -c 0
