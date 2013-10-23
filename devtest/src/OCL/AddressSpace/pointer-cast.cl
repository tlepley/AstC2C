// -> global variables
__constant int *__constant p_constant;
__global   int *__constant p_global;
__local    int *__constant p_local;
__private  int *__constant p_private;
           int *__constant p_private_std;

void f(
	// -> parameters
	__constant int * q_constant;
	__global   int * q_global,
	__local    int * q_local,
	__private  int * q_private,
	int * q_private_std
	) {
  // -> local variables
  __constant int * r_constant;
  __global   int * r_global;
  __local    int * r_local;
  __private  int * r_private;
  	     int * r_private_std;

	     // Constant address space
	     (__constant int *)p_constant;
	     (__constant int *)q_constant;
	     (__constant int *)r_constant;

	     // Global address space
	     (__global int *)p_global;
	     (__global int *)q_global;
	     (__global int *)r_global;

	     // Local address space
	     (__local int *)p_local;
	     (__local int *)q_local;
	     (__local int *)r_local;

	     // Private address space
	     (__private int *)p_private;
	     (__private int *)q_private;
	     (__private int *)r_private;
	     (__private int *)p_private_std;
	     (__private int *)q_private_std;
	     (__private int *)r_private_std;
	     (int *)p_private;
	     (int *)q_private;
	     (int *)r_private;
	     (int *)p_private_std;
	     (int *)q_private_std;
	     (int *)r_private_std;
	}

