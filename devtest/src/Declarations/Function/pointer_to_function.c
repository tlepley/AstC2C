//
// By default, pointer to functions must point to internal function
// We have done this choice because we don't have an easy way to
// disctinghuish pointers which point to external functions and pointer
// which point to internal function.
// By the way, it is not allowed to set subsequently a pointer to
// function with internal and external function. The C2C will not
// detect the problem
//



// Simple pointers to functions
int (*p1)();
int (*p2)(int i);


// Pointer to function as a parameter of a function 
// (external)
extern int f1(int (*p)(char));

// function returning a pointer to function
// (external)
extern int (*f2(int i))(int i);


int main() {
  p1();
  p2(2);
  f2(1)(2);
}
