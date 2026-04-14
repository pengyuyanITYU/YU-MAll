local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local refill_tokens = tonumber(ARGV[2])
local refill_period = tonumber(ARGV[3])
local requested = tonumber(ARGV[4])
local now = tonumber(ARGV[5])
local ttl = tonumber(ARGV[6])

local current = redis.call('HMGET', key, 'tokens', 'timestamp')
local tokens = tonumber(current[1])
local timestamp = tonumber(current[2])

if tokens == nil then
    tokens = capacity
    timestamp = now
end

local delta = math.max(0, now - timestamp)
local refill = math.floor(delta * refill_tokens / refill_period)
if refill > 0 then
    tokens = math.min(capacity, tokens + refill)
    timestamp = now
end

local allowed = 0
if tokens >= requested then
    tokens = tokens - requested
    allowed = 1
end

redis.call('HMSET', key, 'tokens', tokens, 'timestamp', timestamp)
redis.call('PEXPIRE', key, ttl)

return {allowed, tokens}
