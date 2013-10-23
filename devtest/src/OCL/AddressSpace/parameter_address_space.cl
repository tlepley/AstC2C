//----------------------------------------------------------------------
// Address spaces for function and kernel parameters must be clarified
// since their meaning is really unclear
//----------------------------------------------------------------------


// By default, function parameters are private 
int function1(int a, int b) {}

// function parameters can be in the local/global/constant address space
// [Note: To be clarified in the specification, what does it mean ??]
int function2(__local int a, __global int b, __constant int c,__private int d) {}

// By default, kernel aguments are private 
kernel void kernel1(int a, int b) {}


kernel void kernel2(__private char i, __global int * __private p1,  __constant int *p2, __local int *p3) {}
