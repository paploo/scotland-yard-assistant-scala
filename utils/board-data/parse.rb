$LOAD_PATH.unshift('./lib')

begin
  require 'byebug'
rescue LoadError => e
  STDERR.puts("WARNING: Could not load `byebug': #{e.class.name}: #{e.message}")
end

require 'set'

require 'start_stations'
require 'route'
require 'parser'

# We have three jobs:
# 1. Normalize each of the input files into memory.
# 2. Cross check the two "untrusted" sources.
# 3. Output normalized form.


routes = [ReineckeParser, AndriuschParser, GeeksamParser].map do |klass|
  klass.new.routes
end

puts routes.map(&:length).inspect

routes.each {|rs| puts rs[0,10].inspect}

puts (routes[0] - routes[1]).length
puts (routes[1] - routes[0]).length

# Need a validator that can self-consistency check (each route has a reverse with the same mode),
# and that can take two routes and compute their difference.
