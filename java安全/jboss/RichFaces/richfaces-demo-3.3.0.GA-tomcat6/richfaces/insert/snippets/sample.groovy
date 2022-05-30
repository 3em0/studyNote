double[] values = new double[8]

i = 0
[1, 2, 3, 4, 5, 6, 7, 8].each {
  println it
  values[i++] = it
  // uncomment the next line and it works fine!!
  //println it
}