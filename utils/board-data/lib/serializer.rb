class Serializer

  def initialize(routes)
    @routes = routes
  end

  def write(io)
    routes.each {|route| io << route.to_a.join(" ")}
  end

end
