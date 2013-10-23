#pragma cl_ST_set_private_variable_length_array

kernel void f(int i) {
  int T[i];
  private int T2[i+1];
}
