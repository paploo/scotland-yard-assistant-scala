$LOAD_PATH.unshift('./lib')

begin
  require 'byebug'
rescue LoadError => e
  STDERR.puts("WARNING: Could not load `byebug': #{e.class.name}: #{e.message}")
end

require 'start_stations'
require 'route'
require 'parser'

# We have three jobs:
# 1. Normalize each of the input files into memory.
# 2. Cross check the two "untrusted" sources.
# 3. Output normalized form.

p = ReineckeParser.new
routes = p.compute_routes
puts routes[0,10].inspect
