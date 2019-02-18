package mono.cecil;

public enum SecurityAction
{
	Request,
	Demand,
	Assert,
	Deny,
	PermitOnly,
	LinkDemand,
	InheritDemand,
	RequestMinimum,
	RequestOptional,
	RequestRefuse,
	PreJitGrant,
	PreJitDeny,
	NonCasDemand,
	NonCasLinkDemand,
	NonCasInheritance
}
