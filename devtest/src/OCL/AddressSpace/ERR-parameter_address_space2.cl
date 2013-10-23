// kernel pointer arguments can only point to __local, __global, __constant address spaces

kernel int kernel1(__private int * a, int *b) {}
