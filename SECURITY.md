# Security policy

Please report token disclosure, credential storage, or authentication bypass issues privately to the repository owner.
Do not include passwords, access tokens, full configuration files, or unredacted logs in public issues.

Passwords are not stored unless the user explicitly enables clear-text storage for a server profile. Access tokens are
stored because they are required for automatic login. The vanilla `servers.dat` file must therefore be treated as
sensitive and must not be attached to a public issue or included in a shared modpack export.
