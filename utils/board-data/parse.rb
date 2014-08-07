$LOAD_PATH.unshift('./lib')

begin
  require 'byebug'
rescue LoadError => e
  STDERR.puts("WARNING: Could not load `byebug': #{e.class.name}: #{e.message}")
end

require 'set'
require 'pp'

require 'start_stations'
require 'route'
require 'parser'
require 'validator'

# We have three jobs:
# 1. Normalize each of the input files into memory.
# 2. Cross check the two "untrusted" sources.
# 3. Output normalized form.


all_routes = [ReineckeParser, AndriuschParser, GeeksamParser].map do |klass|
  klass.new.routes
end

all_routes = [
  ReineckeParser.new.routes.map {|r| r.to_ravensburger},
  GeeksamParser.new.routes
]

puts all_routes.map(&:length).inspect
puts (all_routes.map {|routes| Validator.bidirectional_integrity(routes)}.inspect)
puts (all_routes.map {|routes| Validator.station_list(routes).length}.inspect)

#all_routes.each {|rs| puts rs[0,10].inspect}
#all_routes.each {|routes| puts Validator.bidirectional_integrity(routes).inspect}

(0...all_routes.length).to_a.combination(2).each do |i,j|
  puts "DIFF #{i} and #{j}:"
  puts Validator.diff(all_routes[i], all_routes[j]).pretty_inspect
end

# Diff(Reinecke, Geeksam)
#[[Route(13, 14, :taxi),   Real change, introduced later than the renumbering!
#  Route(14, 13, :taxi),   Real change, introduced later than the renumbering!
#  Route(99, 112, :taxi),  !! Error in Geeksam, 99 112 taxi is on both boards.
#  Route(112, 99, :taxi),  !! Error in Geeksam, 112 99 taxi is on both boards.
#  Route(159, 198, :taxi), !! Error in Geeksam, 159 198 taxi exists on both boards.
#  Route(185, 199, :bus),  This is real, because of a misprint on the MB board where the bus route diverges at 198
#  Route(187, 199, :bus),  # Error in Reinecke. No route here on either board.
#  Route(198, 159, :taxi), !! Error in Geeksam, 159 198 taxi exists on both boards.
#  Route(199, 185, :bus),  This is real, because of a misprint on the MB board where the bus route diverges at 198
#  Route(199, 187, :bus)], # Error in Reinecke. No route here on either board
# [Route(185, 187, :bus),  # Error in Reinecke, exists in both. Legit due to station 198 diverging bus
#  Route(187, 185, :bus),  # Error in Reinecke, exists in both. Legit due to station 198 diverging bus.
#  Route(198, 199, :taxi), Legit change to taxi of what used to be 199 to 185 bu bus (due to misprint of diverging)
#  Route(199, 198, :taxi)]]Legit change to taxi of what used to be 199 to 185 bu bus (due to misprint of diverging)
#
# Question: How best to deal with ambiguous MB board? It's like I need a version with a taxi and a version
# with two buses?
