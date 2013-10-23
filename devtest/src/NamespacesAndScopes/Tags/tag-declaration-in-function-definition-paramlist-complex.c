//#######################################################################
// Tag declaration inside the parameter list of a function definition
//#######################################################################

// Function definition with a pointer to function whose prototype defines type tags
void f2(
	struct S {int i; char j;} * (*f_param)(
					       struct S_proto {
						 union B_proto {
						   int i;
						   char j;
						 } i;
						 int j;
					       } s_proto,
					       enum E {A,B,C=0} e_proto
					       ), 
	enum E {A,B,C=0} e_param  // No overiding, scope is the unction
	) {
  struct S s; // Reference
  enum E e=B; // Reference
}

