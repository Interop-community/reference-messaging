USE oic;

SET AUTOCOMMIT = 0;

START TRANSACTION;

# reference messaging client
DELETE FROM client_grant_type WHERE owner_id = (SELECT id from client_details where client_id = 'messaging_client');
DELETE FROM client_scope WHERE owner_id = (SELECT id from client_details where client_id = 'messaging_client');
DELETE FROM client_details WHERE client_id = 'messaging_client';

COMMIT;

SET AUTOCOMMIT = 1;
