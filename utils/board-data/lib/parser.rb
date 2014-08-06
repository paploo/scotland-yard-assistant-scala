require 'pathname'
require 'set'
require 'json'

class Parser

  def initialize(file)
    @file = Pathname.new(file).expand_path("./data/in/")
  end

  attr_reader :file

  def compute_routes
    []
  end

  def routes
    Set.new(compute_routes).to_a.sort
  end

end

class ReineckeParser < Parser

  TRANSIT_MODE_MAP = {
    "Taxi" => :taxi,
    "Bus" => :bus,
    "Underground" => :underground,
    "Black" => :ferry
  }.freeze

  def self.to_mode(mode)
    TRANSIT_MODE_MAP[mode]
  end

  def initialize(file = 'MiltonBradleyBoard.json')
    super
  end

  def compute_routes
    json = JSON.parse(file.read)

    json.flat_map do |src_station, data|
      src = src_station.to_i
      data.flat_map do |dest_station, raw_modes|
        dest = dest_station.to_i
        modes = raw_modes.map {|m| self.class.to_mode(m)}
        modes = modes - [:ferry] unless modes == [:ferry] # Filter out all the extra black ticket edges that aren't ferries
        modes.map do |mode|
          Route.new(src, dest, mode)
        end
      end
    end
  end

end

class AndriuschParser < Parser
end

class GeeksamParser < Parser
end
