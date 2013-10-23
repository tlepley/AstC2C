//##################################################################
// Address space qualifiers simple test
//   - Test both syntaxes of address spaces
//   - Test all possible 
//##################################################################

// Global scope variables are necessary 'constant' in OCL 
typedef local char T;

constant int c1 = 3;
__constant int c2 = 4;
const __constant int c3 = 5;

void f(int i) { // By default, the argument is private
  int j;  // By default, a local variable is private

  global int g1;
  __global int g2;
  const __global int g3;

  local int l1;
  __local int l2;
  const __local int l3;

  private int p1;
  __private int p2;
  const __private int p3;

  T t1;
  const T t2;

  // Local pointer pointing to the global address space
  __global int * __local q1;
  const __global int * const __local q2;
}
