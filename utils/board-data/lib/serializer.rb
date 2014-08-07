class Serializer

  def self.out_dir
    Pathname.new("./data/out/")
  end

  def initialize(routes)
    @routes = routes
  end

  attr_reader :routes

  def write_filename(filename)
    path = Pathname.new(filename).expand_path(self.class.out_dir)
    path.open('w') do |io|
      write(io)
    end
    path
  end

  def write(io)
    StartStations.to_a.sort.each {|station| io << station << "\n"}
    routes.each {|route| io << [route.source, route.destination, route.transit_mode.to_s.capitalize].join(", ") << "\n"}
  end

end
