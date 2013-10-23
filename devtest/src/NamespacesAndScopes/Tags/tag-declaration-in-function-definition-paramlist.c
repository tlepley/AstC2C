//#######################################################################
// Tag declaration inside the parameter list of a function definition
//#######################################################################


// Function definition (scope corresponding to the whole function)
void f1(
	struct S {
	  union U {
	    int i;
	    char j;
	  } i;
	} s_param
	, enum E {A,B,C=0} e_param
	) {
  struct S s; // Reference to parameter list tag
  union U u;  // Reference to parameter list tag
  enum E e;   // Reference to parameter list tag
  e=B;         // Reference to parameter list symbol
}


