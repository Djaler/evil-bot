ALTER TABLE users
    ADD gender VARCHAR DEFAULT 'IT' NOT NULL;
UPDATE users SET gender = 'MALE' WHERE male = True;
UPDATE users SET gender = 'FEMALE' WHERE male = False;
ALTER TABLE users DROP COLUMN male;