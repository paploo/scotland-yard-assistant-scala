module Validator

  # This checks that for each forward
  # route, there is a reverse route.
  def self.bidirectional_integrity(routes)
    routes.reject {|route| routes.include?(route.reverse)}
  end

  def self.station_list(routes)
    stations = routes.flat_map {|route| [route.source, route.destination]}.uniq.sort
  end

  # This returns a list of subtractions and list of additions between the two sets.
  def self.diff(old, new)
    [
      old - new, #subtractions
      new - old, #additions
    ]
  end

end
