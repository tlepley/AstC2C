// C99, 6.8.4.1 : the controling expression of an if shall have the scalar type

void f() {
  struct {int i;} s;

  if (s) {s.i=1;}
}
