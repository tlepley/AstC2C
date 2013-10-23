//##################################################################
// !! BUG !!
// 
// The parser does not consider the second f as a function prototype
// but as a variable
//##################################################################

int f(int i);
int ((f))(int i);

