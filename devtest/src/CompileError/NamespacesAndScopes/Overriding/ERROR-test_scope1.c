//
// Test that over-riding is correct.
// -> The storage size 'i' must not be known, since A is uncomplete
//

struct A {int i;};

void f() {
  struct A;
  struct A i;
}
