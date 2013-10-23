//##################################################################
// !! BUG !!
// 
// The parameter i is not considered as function parameter, but as
// parameter of a prototype (scope limited to the param list)
//##################################################################

int ((f))(int i) {
  return(i+1);
}

