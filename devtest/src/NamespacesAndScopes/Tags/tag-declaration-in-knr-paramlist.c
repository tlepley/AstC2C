//#######################################################################
// K&R style parameter declaration
//#######################################################################


int f(a,b)
     struct S {int i; int j;} *a;
     char b;
{
  struct S j;  // Reference to the parameter tag
  return(a->i+b);
}

