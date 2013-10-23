// Incomplete array in abstract declarator, should generate an error

int f() {
  void *p1,*p2;
  p1 = (int (*)[][])p2;
}
