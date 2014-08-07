class Route
  include Comparable

  #The (verified correct) re-mapping is:
  #    MB#200 is R#171
  #    MB#171 is R#159
  #    MB#159 is R#128
  #    MB#128 is R#118
  #    MB#118 is R#108
  #
  #    There is no MB#108
  #    There is no R#200
  MB_TO_RAV_MAPPING = {
    200 => 171,
    171 => 159,
    159 => 128,
    128 => 118,
    118 => 108
  }.freeze

  RAV_TO_MB_MAPPING = MB_TO_RAV_MAPPING.freeze

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

  def reverse
    self.class.new(destination, source, transit_mode)
  end

  def to_ravensburger
    Route.new(MB_TO_RAV_MAPPING.fetch(source,source), MB_TO_RAV_MAPPING.fetch(destination, destination), transit_mode)
  end

  def to_miltonbradley
    Route.new(RAV_TO_MB_MAPPING[source], RAV_TO_MB_MAPPING[destination], transit_mode)
  end

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
