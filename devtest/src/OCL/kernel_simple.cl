// [To be clarified in the spec: can we make protos of kernels]

//kernel void kernel_proto1(int i);
//__kernel void kernel_proto2(int i);

kernel void kernel1(int i, int j) {}
__kernel void kernel2(int i, int j) {}

void kernel kernel3(int i, int j) {}
void __kernel kernel4(int i, int j) {}


