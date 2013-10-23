// Variable length array are forbidden in OpenCL C

__kernel void k(int i) {
  int tab[i];
}
