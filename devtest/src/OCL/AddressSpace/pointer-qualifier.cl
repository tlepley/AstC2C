// -> global variables
__constant int *__constant p_constant;
__global   int *__constant p_global;
__local    int *__constant p_local;
__private  int *__constant p_private;
           int *__constant p_private_std;

void k1(
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

  // Pointers to constant
  r_constant=p_constant;
  r_constant=q_constant;
  r_constant=q_constant+3;

  // Pointers to global
  r_global=p_global;
  r_global=q_global;
  r_global=q_global+3;

  // Pointers to local
  r_local=p_local;
  r_local=q_local;
  r_local=q_local+3;

  // Pointers to private
  r_private=p_private;
  r_private=p_private_std;
  r_private=q_private;
  r_private=q_private_std;
  r_private=p_private+3;
  r_private=p_private_std+3;
}
