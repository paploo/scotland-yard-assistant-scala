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
require 'serializer'

# We have three jobs:
# 1. Normalize each of the input files into memory.
# 2. Cross check the two "untrusted" sources.
# 3. Output normalized form.



class App

  def all_routes
    [ReineckeParser, GeeksamParser].map { |klass| klass.new.routes }
  end

  def ravensburger_normalized_routes
    [
      ReineckeParser.new.routes.map {|r| r.to_ravensburger},
      #AndriuschParser.new.routes.map, # I don't know what you are! (You have 200 and 108, which means you are just plain wrong.
      GeeksamParser.new.routes
    ]
  end

  def all_corrected_routes
    [ReineckeParser, GeeksamParser].map { |klass| klass.new.corrected_routes }
  end

  def corrected_ravensburger_normalized_routes
    [
      ReineckeParser.new.corrected_routes.map {|r| r.to_ravensburger},
      GeeksamParser.new.corrected_routes
    ]
  end

  def run
  end

end

class AuditApp < App

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
  #
  # Question Tree:
  # 1. Do you have a station 200? (Is this new or old numbering)
  # 2. Do you play with a taxi line from 199 to 198? (Only needed on boards with a 200 to resolve ambiguity)
  # 3. Do you have a taxi line from 13 to 14?
  def audit(routes_list)
    puts routes_list.map(&:length).inspect
    puts (routes_list.map {|routes| Validator.bidirectional_integrity(routes)}.inspect)
    puts (routes_list.map {|routes| Validator.station_list(routes).length}.inspect)

    #routes_list.each {|rs| puts rs[0,10].inspect}
    #routes_list.each {|routes| puts Validator.bidirectional_integrity(routes).inspect}

    (0...routes_list.length).to_a.combination(2).each do |i,j|
      puts "DIFF #{i} and #{j}:"
      puts Validator.diff(routes_list[i], routes_list[j]).pretty_inspect
    end
  end

  def run
    #audit(ravensburger_normalized_routes())
    audit(corrected_ravensburger_normalized_routes())
    #audit(all_routes)
  end

end

class WriterApp

  def run
    mapping.each do |parserklass, filename|
      routes = parserklass.new.corrected_routes
      serializer = Serializer.new(routes)
      path = serializer.write_filename(filename)
      puts path.inspect
    end
  end

  def mapping
    {
      GeeksamParser => 'ravensburger.csv',
      ReineckeParser => 'miltonbradley.csv'
    }
  end

end


#AuditApp.new.run
WriterApp.new.run
