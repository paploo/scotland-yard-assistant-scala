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
    compute_routes.uniq.sort
  end

  def corrected_routes
    routes()
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

  def corrected_routes
    removals = [
      Route.new(187, 199, :bus),
      Route.new(199, 187, :bus)
    ]
    additions = [
      Route.new(185, 187, :bus),
      Route.new(187, 185, :bus)
    ]
    (routes - removals + additions).uniq.sort
  end

end

class AndriuschParser < Parser

  TRANSIT_MODE_MAP = {
    "T" => :taxi,
    "B" => :bus,
    "U" => :underground,
    "X" => :ferry
  }.freeze

  def self.to_mode(mode)
    TRANSIT_MODE_MAP[mode]
  end

  def initialize(file = 'routes.txt')
    super
  end

  def compute_routes
    file.each_line.flat_map do |line|
      raw_src, raw_dest, raw_mode = line.chomp.split(/\s+/)
      if raw_src == "0" && raw_dest == "0" # Why would he have "0 0 T" in his data?
        []
      else
        [
          Route.new(raw_src.to_i, raw_dest.to_i, self.class.to_mode(raw_mode)),
          Route.new(raw_dest.to_i, raw_src.to_i, self.class.to_mode(raw_mode))
        ]
      end
    end
  end

  def corrected_routes
    raise NotImplementedError("This file is too out of whack to correct--it has both 108 and 200!")
  end

end

class GeeksamParser < Parser

  TRANSIT_MODE_MAP = {
    :yellow => :taxi,
    :green => :bus,
    :red => :underground,
    :black => :ferry
  }.freeze

  def self.to_mode(mode)
    TRANSIT_MODE_MAP[mode]
  end

  def initialize(file = 'full_board.rb')
    super
  end

  def compute_routes
    data.flat_map do |raw_src, routing|
      routing.flat_map do |raw_mode, dests|
        dests.map do |raw_dest|
          Route.new(raw_src.to_i, raw_dest.to_i, self.class.to_mode(raw_mode))
        end
      end
    end
  end

  def corrected_routes
    removals = [
    ]
    additions = [
      Route.new(99, 112, :taxi),
      Route.new(112, 99, :taxi),
      Route.new(159, 198, :taxi),
      Route.new(198, 159, :taxi)
    ]
    (routes - removals + additions).uniq.sort
  end

  def data
    @data ||= instance_eval(file.read)
  end

end
