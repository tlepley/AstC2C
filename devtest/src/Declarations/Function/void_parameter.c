//
// void parameter: must disappear when instering the 'this' parameter
// Otherwise, it's not C anymore
//

extern int f(void);

int f(void) {
  return(1);
}
int g(void) {
  return(2);
}
