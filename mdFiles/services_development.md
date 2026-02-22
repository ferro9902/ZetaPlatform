# Services Development <img src="../mdImgs/aruba.png" alt="arubapec" width="100" align="right"/>

This document covers the services development of the Platform Zeta.

Auth service should ideally act purely as an Identity Provider (IdP) and Credential Store. Its single responsibility is verifying who the user is (authentication) and managing access tokens.