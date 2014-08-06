class Route
  include Comparable

  TRANSIT_MODE_RANKS = {
    :taxi => 1,
    :bus => 2,
    :underground => 4,
    :ferry => 8
  }.freeze

  TRANSIT_MODES = TRANSIT_MODE_RANKS.keys.freeze

  def self.mode_rank(transit_mode)
    case transit_mode
    when Numeric
      transit_mode.to_i
    when Symbol, String
      TRANSIT_MODE_RANKS[transit_mode]
    else
      nil
    end
  end

  def initialize(source, destination, transit_mode)
    @source = source
    @destination = destination
    @transit_mode = transit_mode
  end

  attr_reader :source, :destination, :transit_mode

  def to_a
    @to_a ||= [source, destination, self.class.mode_rank(transit_mode)]
  end

  def to_s
    "Route(#{source.inspect}, #{destination.inspect}, #{transit_mode.inspect})"
  end

  def inspect
    "Route(#{source.inspect}, #{destination.inspect}, #{transit_mode.inspect})"
  end

  def hash
    @hash ||= to_a.hash
  end

  def <=>(other)
    case other
    when Route
      self.to_a <=> other.to_a
    else
      false
    end
  end

  def eql?(other)
    case other
    when Route
      self == other
    else
      false
    end
  end

end
