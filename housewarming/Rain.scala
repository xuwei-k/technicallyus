import processing.core._
import processing.core.scala._
import PConstants._
import PApplet._
class Rain extends SApplet { 
lazy val px = new DrawProxy { 
/**
 * It's raining! Please draw us a house.
 *
 * <p>(Before Saturday <b>September 13th at 9 p.m.</b>,
 * because that is when <br> we're having a party at
 * <a href="http://maps.google.com/maps?f=q&hl=en&geocode=&q=96+Schermerhorn+Street,+apartment+3G,+in+Brooklyn&sll=40.6904,-73.98994&sspn=0.011422,0.012853&ie=UTF8&z=16&iwloc=addr">
   96 Schermerhorn Street</a>, apartment 3G, <br> in Brooklyn. 
 * See you there!)
 */

size(400, 400)
smooth()

val us = loadImage("us.jpg")
val us_wet = loadImage("us.jpg")
us_wet.filter(GRAY)

val us_x = width / 2 - us.width / 2
val us_y = height - us.height

var drops = cloud

case class Drop(x: Float, y: Float, cx: Float, cy: Float, spunk: Float) {
  def draw() { line(x, y, x - cx, y - cy) }
  def move = 
    Drop(x + cx, y + cy, 
      cx + random(-.1, .1) + .1*(0 - cx), cy + .1*(spunk - cy), spunk)
  def next = {
    val fall = move
    house.find(_.passes(fall)).map(bounce(_)).getOrElse(fall)
  }
  def speed = Math.sqrt(cx*cx + cy*cy)
  def bounce(l: Line) = (
      if (l.m == 0) Drop(x, y, random(-3, 3), -1 * cy, spunk)
      else Drop(x, y, (1 / l.m).max(-spunk/2).min(spunk/2), -.5 * cy, spunk)
    ).move
}

def cloud = List.tabulate(3, n =>
  Drop(random(width).toInt, 0, 0, 3, random(2, 5))
)

def rain = cloud ++ 
  drops.map(_.next).filter( d => d.y < height && d.speed > .1)
  
def touches(ds: List[Drop]) = 
  ds exists { d => d.y > us_y && d.x > us_x && d.x < width - us_x }

case class Line (x1: Int, y1:Int, x2:Int, y2:Int) {
  def draw() { line(x1, y1, x2, y2) }
  val top = min(y1, y2)
  val left = min(x1, x2)
  val right = max(x1, x2)
  val m = (y2 - y1).toFloat / (x2 - x1)
  val b = y1 - m*x1
  def passes(d: Drop) =
    d.y >= top && d.x >= left && d.x <= right && {
      val y = (m*d.x + b)
      d.y >= y && (d.y - d.cy) < y
    }
}

var house = Set[Line]()

var cur: Option[Line] = None

override def mousePressed {
  cur = Some(Line(mouseX, mouseY, mouseX, mouseY))
}

override def mouseDragged {
  cur = cur.map(c => Line(c.x1, c.y1, mouseX, mouseY))
}

override def mouseReleased {
  house = house ++ cur.toList
  cur = None
}

def draw() {
  cursor(CROSS)
  strokeWeight(1.5)
  background(255)
  drops = rain
  image(if (touches(drops)) us_wet else us, us_x, us_y)
  stroke(100)
  drops.foreach(_.draw())
  strokeWeight(3);
  stroke(50)
  house.foreach(_.draw())
  stroke(200)
  cur.foreach(_.draw())
}
} } 
