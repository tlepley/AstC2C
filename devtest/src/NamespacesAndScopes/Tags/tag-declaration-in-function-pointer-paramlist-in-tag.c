//#######################################################################
// Tag declaration inside parameters list of a pointer to function,
// itself being the member of a tag
//#######################################################################

struct D {
  struct F {int i;} u;
  int (*f)(struct B{int i;} a, struct B b);
  int b;
};
