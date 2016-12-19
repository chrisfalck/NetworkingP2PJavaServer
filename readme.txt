Unfortunately, we ran our of time on the project, and it's only partially
complete. There are plenty of organizational changes we'd like to make but
didn't have time to implement. I'm going to leave this up for now, and maybe
return to finish the unimplemented features in the future. Right now, this
essentially acts as a file server, by breaking a file into pieces and then 
distributing it to client threads on other computers that reconstruct the 
file upon receiving all pieces. Some P2P protocol is followed prior to the 
file transfer, but some still needs to be written/fixed.
