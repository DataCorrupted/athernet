function [ wave ] = receive( t, sampling_rate )
%% receive: Given a specified time it will record arell the sound
%  and output it .

    if nargin < 2   sampling_rate = 44100;  end;
    if nargin < 1   t = 10;                 end;

    recorder = audiorecorder(sampling_rate, 24, 1);
    recordblocking(recorder, t);
    wave = recorder;

end

