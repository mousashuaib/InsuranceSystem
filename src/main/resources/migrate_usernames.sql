UPDATE clients 
SET username = SUBSTRING(email FROM 1 FOR POSITION('@' IN email) - 1)
WHERE username IS NULL;
