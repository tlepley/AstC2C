// Kernel parameters must be in the private address space
// The kernel must return 'void'
//
// These OpenCL errors should not be checked since the file has a .c extension

__kernel int kernel1(__local int a, __global int b, __constant int c) {}
