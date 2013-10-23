// Forbidden vector literal

int f() {
  struct S{int i;} s;
  int4 i4=(int4)(1,2,s);
}
